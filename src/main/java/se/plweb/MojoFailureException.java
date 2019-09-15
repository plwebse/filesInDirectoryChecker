package se.plweb;

public class MojoFailureException extends Exception {

    public MojoFailureException(String string) {
        super(string);
    }

    public MojoFailureException(String message, Throwable e) {
        super(message, e);
    }

}
