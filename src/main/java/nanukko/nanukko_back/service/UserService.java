package nanukko.nanukko_back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.domain.user.Kid;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.user.KidInfoDTO;
import nanukko.nanukko_back.dto.user.UserInfoDTO;
import nanukko.nanukko_back.repository.KidRepository;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final KidRepository kidRepository;

    //사용자의 내 정보 조회
    public UserInfoDTO getUserInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //모든 자녀 조회
        List<Kid> kids = kidRepository.findByUserOrderByKidId(user);
        List<KidInfoDTO> kidInfoDTOS = kids.stream()
                .map(kid -> KidInfoDTO.builder()
                        .kidId(kid.getKidId())
                        .kidBirth(kid.getKidBirth())
                        .kidGender(kid.isKidGender())
                        .build())
                .toList();

        return UserInfoDTO.builder()
                .userId(user.getUserId())
                .password(user.getPassword())
                .userBirth(user.getUserBirth())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .gender(user.isGender())
                .addrMain(user.getAddress().getAddrMain())
                .addrDetail(user.getAddress().getAddrDetail())
                .addrZipcode(user.getAddress().getAddrZipcode())
                .score(user.getScore())
                .profile(user.getProfile())
                .kids(kidInfoDTOS)  // 모든 자녀 정보 포함
                .build();
    }
    
    //사용자의 내 정보 수정





}
