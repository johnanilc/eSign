/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.util.Date;

/**
 *
 * @author johnanilc
 */
public class UserSession {
    private User user = null;
    private String httpSessionId = "";
    private Date lastAccessedTime = null;
    
    public UserSession(User user, String httpSessionId, Date lastAccessedTime){
        this.user = user;
        this.httpSessionId = httpSessionId;
        this.lastAccessedTime = lastAccessedTime;
    }
    
    public User getUser(){
        return user;
    }
}
