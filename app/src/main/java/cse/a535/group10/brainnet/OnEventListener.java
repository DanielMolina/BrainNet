package cse.a535.group10.brainnet;

public interface OnEventListener<Boolean> {
    public void onSuccess(Boolean object);

    public void onFailure(Exception e);
}
