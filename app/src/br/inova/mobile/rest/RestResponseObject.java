package br.inova.mobile.rest;

public class RestResponseObject {
        
        private boolean success;
        private String  message;
        
        public RestResponseObject() {}
        
        public RestResponseObject(boolean success, String message) {
                this.success = success;
                this.message = message;
        }
        
        public String getMessage() {
                return message;
        }
        
        public boolean isSuccess() {
                return success;
        }
        
        public void setMessage(String message) {
                this.message = message;
        }
        
        public void setSuccess(boolean success) {
                this.success = success;
        }
        
        @Override
        public String toString() {
                return "RestResponseObject [success=" + success + ", message=" + message + "]";
        }
        
}
