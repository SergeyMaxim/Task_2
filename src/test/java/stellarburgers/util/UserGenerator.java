package stellarburgers.util;

import stellarburgers.model.User;

import java.util.UUID;

public class UserGenerator {
    public static User getUniqueUser() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return new User(
                "user_" + uniqueId + "@yandex.ru",
                "pass_" + uniqueId,
                "Name_" + uniqueId
        );
    }
}