package com.nexusplay.controller;

import com.nexusplay.entity.User;




public interface DashboardController {
    void setCurrentUser(User user);
    User getCurrentUser();
}




