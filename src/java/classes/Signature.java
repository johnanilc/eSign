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
public class Signature {
        private String signatureId = "";
        private int page = 0;
        private int left = 0;
        private int top = 0;
        
        public Signature(String signatureId, int page){
            this.signatureId = signatureId;
            this.page = page;
        }
        
        public String getSignatureId(){
            return signatureId;
        }
        
        public int getPage(){
            return page;
        }
        
        public int getLeft(){
            return left;
        }
        
        public int getTop(){
            return top;
        }
        
        public void setLeft(int left){
            this.left = left;
        }
        
        public void setTop(int top){
            this.top = top;
        }
}
