package cz.activecode.dl.services;

import cz.activecode.dl.ibridge.IBridge;

import java.util.List;

/**
 * Bridges operations service.
 */
public interface BridgeService {

    /**
     * Returns all available bridges in probe order.
     * @return all available bridges in probe order
     */
    List<IBridge> getAllBridges();

}
