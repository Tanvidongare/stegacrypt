package com.stegacrypt.service;

import com.stegacrypt.util.RSAUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DemoUserService {

    private final List<DemoUserRecord> demoUsers = new ArrayList<>();

    @PostConstruct
    public void initializeDemoUsers() throws Exception {
        if (!demoUsers.isEmpty()) {
            return;
        }

        demoUsers.add(createDemoUser("abhishek.sushant.chaskar", "Abhishek Sushant Chaskar", "Project Member", "Uses secure sharing as a seeded project account."));
        demoUsers.add(createDemoUser("aditya.atul.deshpande", "Aditya Atul Deshpande", "Project Member", "Uses secure sharing as a seeded project account."));
        demoUsers.add(createDemoUser("tanvi.dongare", "Tanvi Dongare", "Project Member", "Uses secure sharing as a seeded project account."));
        demoUsers.add(createDemoUser("atharva.abhijeet.mahalkar", "Atharva Abhijeet Mahalkar", "Project Member", "Uses secure sharing as a seeded project account."));
        demoUsers.add(createDemoUser("prakhar.kumar.vishawakarma", "Prakhar Kumar Vishawakarma", "Project Member", "Uses secure sharing as a seeded project account."));
    }

    public List<Map<String, Object>> getDemoUsers() {
        List<Map<String, Object>> response = new ArrayList<>();
        for (DemoUserRecord user : demoUsers) {
            response.add(user.toMap());
        }
        return Collections.unmodifiableList(response);
    }

    private DemoUserRecord createDemoUser(String id, String name, String role, String description) throws Exception {
        KeyPair keyPair = RSAUtil.generateKeyPair();
        String publicKeyPem = RSAUtil.publicKeyToPem(keyPair.getPublic());
        String privateKeyPem = RSAUtil.privateKeyToPem(keyPair.getPrivate());
        String fingerprint = RSAUtil.getKeyFingerprint(keyPair.getPublic());

        return new DemoUserRecord(
            id,
            name,
            role,
            description,
            publicKeyPem,
            privateKeyPem,
            fingerprint
        );
    }

    private record DemoUserRecord(
        String id,
        String name,
        String role,
        String description,
        String publicKey,
        String privateKey,
        String fingerprint
    ) {
        private Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("name", name);
            map.put("role", role);
            map.put("description", description);
            map.put("publicKey", publicKey);
            map.put("privateKey", privateKey);
            map.put("fingerprint", fingerprint);
            return map;
        }
    }
}
