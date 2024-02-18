package cn.dbj.domain.userInfo.model.factory;

import cn.dbj.domain.userInfo.model.aggregate.User;
import cn.dbj.domain.userInfo.repository.IUserRepository;
import cn.dbj.framework.starter.distributedid.core.snowflake.Snowflake;
import cn.dbj.framework.starter.distributedid.toolkit.SnowflakeIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class UserFactory {
    private final IUserRepository userRepository;
    public static User creatUser(String name, String imageUrl, Date birthday, Integer age, Integer sex){
        Snowflake snowflake = new Snowflake(12);
        Long id = snowflake.nextId();

        String randomLetters = generateRandomLetters(5);
        // Extract last five digits of id
        String idSuffix = String.valueOf(id).substring(Math.max(0, String.valueOf(id).length() - 5));
        String nickName = randomLetters + idSuffix;
        User user = new User(id, nickName , name, imageUrl, birthday, age, sex);
        return user;
    }

    public static User creatUserTest(Long id , String name, String imageUrl, Date birthday, Integer age, Integer sex){
        String randomLetters = generateRandomLetters(5);
        // Extract last five digits of id
        String idSuffix = String.valueOf(id).substring(Math.max(0, String.valueOf(id).length() - 5));
        String nickName = randomLetters + idSuffix;
        User user = new User(id, nickName , name, imageUrl, birthday, age, sex);
        return user;
    }

    private static String generateRandomLetters(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = (char) (random.nextInt(26) + 'a');
            if (random.nextBoolean()) { // Randomly choose to make it uppercase
                c = Character.toUpperCase(c);
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
