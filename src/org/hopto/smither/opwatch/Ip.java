package org.hopto.smither.opwatch;

import java.io.Serializable;

class Ip implements Serializable{
    private static final long serialVersionUID = 1L;
    private String ip;
    
    Ip(String ip){
        if (ip.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")){
            this.ip=ip;
        }else{
            throw new IllegalArgumentException();
        }
    }
    Ip(int a, int b, int c, int d){
        this.ip=a+"."+b+"."+c+"."+d;
    }
    Ip(byte[] ip){
        this.ip=(((int)ip[0])+256)%256+"."+(((int)ip[1])+256)%256+"."+(((int)ip[2])+256)%256+"."+(((int)ip[3])+256)%256;
    }
    
    @Override
    public String toString(){
        return ip;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Ip))
            return false;
        Ip other = (Ip) obj;
        if (ip == null) {
            if (other.ip != null)
                return false;
        } else if (!ip.equals(other.ip))
            return false;
        return true;
    }
    
    String getIp(){
        return this.ip;
    }
    
    
}
