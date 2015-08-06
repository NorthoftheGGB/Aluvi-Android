package com.aluvi.android.managers;

import java.util.ArrayList;

/**
 * Created by usama on 8/2/15.
 */
public class RequestQueue {
    public interface RequestQueueListener {
        void onRequestsFinished();

        void onError(String message);
    }

    private ArrayList<Task> requests = new ArrayList<>();
    private int completedTasks;
    private RequestQueueListener listener;

    public RequestQueue(RequestQueueListener listener) {
        this.listener = listener;
    }

    public RequestQueue addRequest(Task request) {
        requests.add(request);
        return this;
    }

    public void execute() {
        for (Task task : requests) {
            task.setListener(new Task.TaskListener() {
                @Override
                public void onComplete() {
                    if (updateCompletedTasks())
                        listener.onRequestsFinished();
                }

                @Override
                public void onError(String message) {
                    listener.onError(message);
                    cancelTasks();
                }
            });

            task.run();
        }
    }

    public void cancelTasks() {
        for (Task task : requests)
            task.cancel();
    }

    public synchronized boolean updateCompletedTasks() {
        completedTasks++;
        return completedTasks >= requests.size();
    }

    public abstract static class Task {
        public interface TaskListener {
            void onComplete();

            void onError(String message);
        }

        private TaskListener listener;
        private boolean isCancelled;

        public abstract void run();

        public void setListener(TaskListener listener) {
            this.listener = listener;
        }

        public void onComplete() {
            if (!isCancelled)
                listener.onComplete();
        }

        public void onError(String message) {
            if (!isCancelled)
                listener.onError(message);
        }

        public void cancel() {
            isCancelled = true;
        }
    }
}
