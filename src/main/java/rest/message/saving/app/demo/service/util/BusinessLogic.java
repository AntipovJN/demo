package rest.message.saving.app.demo.service.util;

import static java.lang.Math.random;

public class BusinessLogic {

    private static void sleepAndRandomThrowRuntimeException(int seconds, int exceptionProbability) {
        try {
			Thread.sleep((long) (seconds * 1000 * random()));
        } catch (InterruptedException e) {}

        int randomProc = (int) (100 * random());
        if (exceptionProbability > randomProc) throw new RuntimeException();
    }

    public static void doSomeWorkOnNotification() {
        sleepAndRandomThrowRuntimeException(2, 10);
    }

    public static void doSomeWorkOnCommentCreation() {
        sleepAndRandomThrowRuntimeException(1, 30);
    }
}
