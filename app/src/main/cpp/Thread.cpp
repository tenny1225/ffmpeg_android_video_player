#include <pthread.h>
#include <jni.h>

//
// Created by tenny on 17-4-16.
//
class Thread {
private:
    pthread_t t;
    static void *thread_start(void *arg) {
        Thread *thread = (Thread *) arg;
        thread->run();
        return (void *)1;
    }

public:

    virtual void run()= 0;

    int start() {
        pthread_detach(t);
        if (pthread_create(&t, NULL, &thread_start, this) != 0) {
            return -1;
        }
        return 0;
    }
};

