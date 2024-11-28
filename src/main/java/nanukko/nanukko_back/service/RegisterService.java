package nanukko.nanukko_back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.user.Kid;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.user.KidInfoDTO;
import nanukko.nanukko_back.dto.user.UserInfoDTO;
import nanukko.nanukko_back.repository.KidRepository;
import nanukko.nanukko_back.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RegisterService {

    private final UserRepository userRepository;
    private final KidRepository kidRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public void registerUser(UserInfoDTO dto) {
        // 1. 존재하는 회원인지 검사
        if (userRepository.existsById(dto.getUserId())) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }


        // 비밀번호 암호화
        dto.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        // 2. UserDTO -> User Entity 변환 및 저장
        User user = dto.toEntity(); // UserDTO에서 toEntity 호출

        userRepository.save(user);

        // 3. KidDTO -> Kid Entity 변환 및 저장
        List<KidInfoDTO> kids = dto.getKids();
        if (!kids.isEmpty()) {
            // KidDTO -> Kid Entity 변환
            List<Kid> kidList = kids.stream()
                    .map(kidDTO -> kidDTO.toEntity(user, kidRepository)) // User 엔티티를 전달
                    .collect(Collectors.toList());

            // Kid Entity 저장
            kidList.forEach(kidRepository::save);
        }
    }

    public boolean existsById(String id){
        return userRepository.existsById(id);
    }
}
