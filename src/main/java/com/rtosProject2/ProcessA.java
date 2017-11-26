package com.rtosProject2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * CLASS ProcessA
 *
 * ProcessA moves each train and submits their positions to bufferAB
 */
public class ProcessA extends ProcessBase {

    private final int _delayMs;
    private final DoubleBuffer<Message> _buffer;
    private final Display _display;
    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private final int maxConsecutiveHalts = 2;
    private int consecutiveHalts = 0;
    private Plane.Movement lastHalted = Plane.Movement.NotSet;
    private static Random _random = new Random();

    /**
     * Constructor that accepts the motion delay (0 for single step mode),
     * the double buffer to which it should submit a 3D array of planes containing each train's position,
     * the Display instance (grid view) and the Console view (log view)
     * @param delayMs
     * @param buffer
     * @param display
     * @param console
     */
    public ProcessA(String name, int delayMs, DoubleBuffer<Message> buffer, Display display, Console console) {
        super(name, console);
        _buffer = buffer;
        _display = display;
        _delayMs = delayMs;
    }

    /**
     * Starts ProcessA thread and continues moving trains until the number of seconds set in Display.Seconds
     * is exhausted. Each time a second passes, the positions of all trains are put into a 3D array
     * and pushed to bufferAB.
     */
    @Override
    public void run() {
        if (_delayMs == 0) ConsoleWriteLine("SINGLE STEP MODE - PRESS ENTER TO PROGRESS\r\n");

        int counter = 0;
        int totalPlanes =  _display.GetPlanes().size();

        //position of all trains is held in the display instance (grid view)
        preventCollision(new Positions(_display.GetPlanes()));
        _buffer.push(new Message<>(Message.PayloadType.Position, new Positions(_display.GetPlanes())));

        //continue moving trains for _display.seconds
        while (counter < _display.Seconds) {
            System.out.println("A PUSHED: " + counter);
            if (_delayMs > 0) {
                try {
                    Thread.sleep(_delayMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                //if delay is 0, then single step
                _display.AwaitKeypress();
            }
             counter++;

            //move each train in the array, update the display
            //and update current state
            for (int i = 0; i < totalPlanes; i++) {
                _display.GetPlanes().get(i).Move();
            }
            try {
                _display.Refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Plane.haltX = false;
            Plane.haltY = false;
            Plane.haltZ = false;

            //push the current state of all planes to bufferAB.
            preventCollision(new Positions(_display.GetPlanes()));
            _buffer.push(new Message<>(Message.PayloadType.Position, new Positions(_display.GetPlanes())));

            //check for messages shuttled from C
            CheckForMessagesFromC();
        }

        //once time has expired, tell the buffer no more data is coming
        //this is necessary so processB will know when to stop trying to retrieve data.
        _buffer.startShutdown();

        while(CheckForMessagesFromC()) {
            //exhaust the test mail messages received from C
        }

        System.out.println("A FINISHED - NO MORE INBOUND VALUES");
    }

    /**
     * This should be rewritten in Project 3 to receive control messages from process C...
     * stub is here for proof of concept. Uncomment call to CheckForMessagesFromC to see how this works.
     */
    private boolean CheckForMessagesFromC() {
        Message message = tryGetMessage();
        if (message != null) {
            if (message.getPayloadType() == Message.PayloadType.Test) {
                String testMsgFromC = (String) message.getPayload();
                System.out.println(
                        "*** [" +
                        message.getPayloadType() + "] MAIL RECEIVED ON [" + name() + "] --- [" + testMsgFromC + "]");
            }
            return true;
        }
        return false;
    }


    /**
     * Returns a number between 1 and 100
     * @return
     */
    private int getRandom() {
        return _random.nextInt(100 + 1) + 1;
    }

    /**
     * This process uses the Collision Management class to create sub threads of the process
     * High level the algorithm will take a base line of all the trains moving
     * If their is no collision, then nothing to do, the other threads are cancelled.
     *
     * If there is a collision each thread returns a distance.
     * Some comparisons are made to see which train to halt based on which distance was greatest
     *
     * Low level:  The class uses the callable class and registers a future.  This allows for non-blocking
     * code and allows the program to execute.  The only blocking code is when the results are needed before the program
     * can proceed.
     *
     * The Class uses a thread pool and returns system resources when not used.
     **/
    private void preventCollision(Positions positions) {

        try {

            // How far we want to look ahead
            int look_ahead = Plane.rows;

            // Uses four instances of the callable class
            Callable<Integer> base_line = new CollisionManagement(positions, Plane.Movement.All, look_ahead);
            Callable<Integer> callable_x = new CollisionManagement(positions, Plane.Movement.X, look_ahead);
            Callable<Integer> callable_y = new CollisionManagement(positions, Plane.Movement.Y, look_ahead);
            Callable<Integer> callable_z = new CollisionManagement(positions, Plane.Movement.Z, look_ahead);

            // This is a java thing...  Future is non-blocking
            Future<Integer> future_baseline = pool.submit(base_line);
            Future<Integer> future_halt_x = pool.submit(callable_x);
            Future<Integer> future_halt_y = pool.submit(callable_y);
            Future<Integer> future_halt_z = pool.submit(callable_z);

            // If baseline is desired, cancel other thread
            final int collision = future_baseline.get();
            if (collision >= look_ahead) {
                future_halt_x.cancel(true);
                future_halt_y.cancel(true);
                future_halt_z.cancel(true);
            } else {             // Else:  calculate which one to use
                ArrayList<tuple> halt = new ArrayList<>();
                halt.add(new tuple(future_halt_x.get(), "X"));
                halt.add(new tuple(future_halt_y.get(), "Y"));
                halt.add(new tuple(future_halt_z.get(), "Z"));
                int multipleFailures = 0;
                Collections.sort(halt);
                Collections.reverse(halt);

                /**
                 * We put every plane's marker and the future value inside an Arraylist of tuples. Then we sort the Arraylist in descending order.
                 * From there we loop through every tuple inside the Arraylist and try and stop the highest weight. If this fails then we need to stop the second highest weight. If that fails then
                 * we have an unavoidable collision.
                 */
                for(tuple t : halt) {
                    String msg = "Sending stop signal to train " + t.getPlane();
                    ConsoleWriteLine(msg);
                    if (t.getPlane().equals("X")) {
                        if (getRandom() > 10) { // Checks for the failure
                            if (lastHalted == Plane.Movement.X) {
                                consecutiveHalts++;
                            } else {
                                consecutiveHalts = 0;
                                lastHalted = Plane.Movement.X;
                            }
                            Plane.haltX = consecutiveHalts < maxConsecutiveHalts;
                        } else {
                            ConsoleWriteLine("Plane X failed to stop.");
                            multipleFailures += 1;
                        }
                    }else if (t.getPlane().equals("Y")) {
                        if (getRandom() > 5) {  // Checks for the failure
                            if (lastHalted == Plane.Movement.Y) {
                                consecutiveHalts++;
                            } else {
                                consecutiveHalts = 0;
                                lastHalted = Plane.Movement.Y;
                            }
                            Plane.haltY = consecutiveHalts < maxConsecutiveHalts;
                        } else {
                            ConsoleWriteLine("Plane Y failed to stop.");
                            multipleFailures += 1;
                        }
                    } else if (t.getPlane().equals("Z")) {
                        if (getRandom() > 1) {  // Checks for the failure
                            if (lastHalted == Plane.Movement.Z) {
                                consecutiveHalts++;
                            } else {
                                consecutiveHalts = 0;
                                lastHalted = Plane.Movement.Z;
                            }
                            Plane.haltZ = consecutiveHalts < maxConsecutiveHalts;
                        } else {
                            ConsoleWriteLine("Plane Z failed to stop.");
                            multipleFailures += 1;
                        }
                    }
                    if (Plane.haltX || Plane.haltY || Plane.haltZ) { break;} // We've already stopped one plane so we can exit.
                }

                if (multipleFailures > 1){
                    ConsoleWriteLine("Multiple failures occurred. Unavoidable collision could possibly happen.");
                }
            }
            if (consecutiveHalts >= maxConsecutiveHalts) {
                lastHalted = Plane.Movement.NotSet;
                consecutiveHalts = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

