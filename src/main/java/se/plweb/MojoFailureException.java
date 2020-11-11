package se.plweb;

class MojoFailureException extends Exception {

    public MojoFailureException(String message, Throwable e) {
        super(message, e);
    }

}
