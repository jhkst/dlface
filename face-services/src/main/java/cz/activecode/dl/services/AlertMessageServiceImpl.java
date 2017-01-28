package cz.activecode.dl.services;

import cz.activecode.dl.to.AlertMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AlertMessageServiceImpl implements AlertMessageService {

    private Lock lock = new ReentrantLock();

    private Map<String, AlertMessage> messages = new LinkedHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMessage(AlertMessage alertMessage) {
        lock.lock();
        try {
            messages.put(alertMessage.getId(), alertMessage);
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AlertMessage> readMessages() {
        lock.lock();
        try {
            List<AlertMessage> values = new ArrayList<>(messages.values());
            messages.clear();
            return values;
        } finally {
            lock.unlock();
        }
    }

}
