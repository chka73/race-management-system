package se.cag.labs.usermanager;

import io.swagger.annotations.ApiModelProperty;

/**
 * Created by dawi on 2015-11-20.
 */
public class Token {

    /**
     * Security Token
     */
    @ApiModelProperty(value = "Security Token", required = true)
    private String token;

    public Token() {

    }

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
