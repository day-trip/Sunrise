package com.daytrip.shared;

import com.google.common.base.Throwables;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.net.Proxy;

public class LoginManager {
    public static YggdrasilUserAuthentication login(String email, String password) {
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(email);
        auth.setPassword(password);

        try {
            auth.logIn();
        }
        catch (AuthenticationException e)
        {
            Minecraft.logger.error("-- Login failed!  " + e.getMessage());
            Throwables.propagate(e);
        }

        return auth;
    }

    public static void setData(YggdrasilUserAuthentication auth) {
        Minecraft.getMinecraft().session = new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), auth.getUserType().getName());
    }
}
