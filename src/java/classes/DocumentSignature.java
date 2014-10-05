/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package classes;

/**
 *
 * @author johnanilc
 */

public class DocumentSignature{
    private int documentId = 0;
    private int signerId = 0;
    private int pageNumber = 0;
    private int signLocationX = 0;
    private int signLocationY = 0;
    
    public int getPageNumber(){
        return pageNumber;
    }
    
    public int getSignLocationX(){
        return signLocationX;
    }
    
    public int getSignLocationY(){
        return signLocationY;
    }
}