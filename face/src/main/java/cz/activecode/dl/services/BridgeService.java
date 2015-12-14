package cz.activecode.dl.services;

import cz.activecode.dl.ibridge.IBridge;
import cz.activecode.dl.ibridge.UserActionListener;

import java.util.List;

public interface BridgeService {

    List<IBridge> getAllBridges();

    void addUserActionListener(UserActionListener userActionListener);
}
