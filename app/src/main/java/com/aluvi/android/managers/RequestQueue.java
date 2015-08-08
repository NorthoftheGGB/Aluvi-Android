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

    public static RequestQueue mergeQueues(final RequestQueue q1, final RequestQueue q2,
                                           RequestQueueListener listener) {
        return new RequestQueue(listener)
                .addRequest(new Task() {
                    @Override
                    public void run() {
                        q1.setListener(new RequestQueueListener() {
                            @Override
                            public void onRequestsFinished() {
                                onComplete();
                            }

                            @Override
                            public void onError(String message) {
                                onError(message);
                            }
                        });

                        q1.execute();
                    }
                })
                .addRequest(new Task() {
                    @Override
                    public void run() {
                        q2.setListener(new RequestQueueListener() {
                            @Override
                            public void onRequestsFinished() {
                                onComplete();
                            }

                            @Override
                            public void onError(String message) {
                                onError(message);
                            }
                        });

                        q2.execute();
                    }
                });
    }

    public RequestQueue buildQueue() {
        for (Task task : requests) {
            task.setListener(new Task.TaskListener() {
                @Override
                public void onComplete() {
                    if (updateCompletedTasks() && listener != null)
                        listener.onRequestsFinished();
                }

                @Override
                public void onError(String message) {
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
