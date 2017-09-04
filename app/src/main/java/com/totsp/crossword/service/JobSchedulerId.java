package com.totsp.crossword.service;

// All JobScheduler Job IDs for this application.
//
// Using an enum here since all jobs scheduled by the same uid (not just package) need to be unique.
//
// These need to be stable across app updates.
public enum JobSchedulerId {
    BACKGROUND_DOWNLOAD(10);

    private int id;

    JobSchedulerId(int id) {
        this.id = id;
    }

    int id() {
        return this.id;
    }
}
