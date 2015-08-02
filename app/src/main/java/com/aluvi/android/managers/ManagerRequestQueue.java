package com.aluvi.android.managers;

import java.util.ArrayList;

/**
 * Created by usama on 8/2/15.
 */
public class ManagerRequestQueue {
    public interface RequestQueueListener {
        void onRequestsFinished();

        void onError(String message);
    }

    private ArrayList<RequestTask> requests = new ArrayList<>();
    private int completedTasks;
    private RequestQueueListener listener;

    public ManagerRequestQueue(RequestQueueListener listener) {
        this.listener = listener;
    }

    public ManagerRequestQueue addRequest(RequestTask request) {
        requests.add(request);
        return this;
    }

    public void execute() {
        for (int i = 0; i < requests.size(); i++) {
            RequestTask task = requests.get(i);
            task.setListener(new RequestTask.TaskListener() {
                @Override
                public void onComplete() {
                    if (updateCompletedTasks())
                        listener.onRequestsFinished();
                }

                @Override
                public void onError(String message) {
                    listener.onError(message);
                }
            });

            task.run();
        }
    }

    public synchronized boolean updateCompletedTasks() {
        completedTasks++;
        return completedTasks >= requests.size();
    }

    public abstract static class RequestTask {
        public interface TaskListener {
            void onComplete();

            void onError(String message);
        }

        private TaskListener listener;

        public abstract void run();

        public void setListener(TaskListener listener) {
            this.listener = listener;
        }

        public void onComplete() {
            listener.onComplete();
        }

        public void onError(String message) {
            listener.onError(message);
        }
    }
}
