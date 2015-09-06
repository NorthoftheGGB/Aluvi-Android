package com.aluvi.android.helpers;

import java.util.ArrayList;
import java.util.List;

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

    public RequestQueue() {
    }

    public RequestQueue addRequest(Task request) {
        requests.add(request);
        return this;
    }

    public void execute() {
        buildQueue().executeTasks();
    }

    private void executeTasks() {
        for (Task task : requests)
            task.run();
    }

    public static RequestQueue mergeQueues(final List<RequestQueue> queuesToMerge, RequestQueueListener listener) {
        RequestQueue output = new RequestQueue(listener);
        for (RequestQueue queue : queuesToMerge) {
            final RequestQueue copy = queue;
            output.addRequest(new Task() {
                @Override
                public void run() {
                    copy.setListener(new RequestQueueListener() {
                        @Override
                        public void onRequestsFinished() {
                            onTaskComplete();
                        }

                        @Override
                        public void onError(String message) {
                            onTaskError(message);
                        }
                    });

                    copy.execute();
                }
            });
        }

        return output;
    }

    public RequestQueue buildQueue() {
        for (Task task : requests) {
            task.setListener(new Task.TaskListener() {
                @Override
                public void onTaskComplete() {
                    if (updateCompletedTasks() && listener != null)
                        listener.onRequestsFinished();
                }

                @Override
                public void onTaskError(String message) {
                    if (listener != null)
                        listener.onError(message);

                    cancelTasks();
                }
            });
        }

        return this;
    }

    public void cancelTasks() {
        for (Task task : requests)
            task.cancel();
    }

    public synchronized boolean updateCompletedTasks() {
        completedTasks++;
        return completedTasks >= requests.size();
    }

    public RequestQueueListener getListener() {
        return listener;
    }

    public void setListener(RequestQueueListener listener) {
        this.listener = listener;
    }

    public abstract static class Task {
        public interface TaskListener {
            void onTaskComplete();

            void onTaskError(String message);
        }

        private TaskListener listener;
        private boolean isCancelled;

        public abstract void run();

        public void setListener(TaskListener listener) {
            this.listener = listener;
        }

        public void onTaskComplete() {
            if (!isCancelled)
                listener.onTaskComplete();
        }

        public void onTaskError(String message) {
            if (!isCancelled)
                listener.onTaskError(message);
        }

        public void cancel() {
            isCancelled = true;
        }
    }
}
