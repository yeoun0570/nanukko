package nanukko.nanukko_back.config;

import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.dto.product.ProductRequestDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

//스프링 빈 등록등을 위한 스프링 설정
@Configuration
@ComponentScan(
        basePackages = "nanukko.nanukko_back"
)
public class ModelMapperConfig {
    //ModelMapper 를 스프링의 빈으로 설정
    @Bean
    public ModelMapper getMapper(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.typeMap(Product.class, ProductRequestDto.class)
                .addMappings(mapper -> mapper.map(Product::getProductId, ProductRequestDto::setId));
        return  modelMapper;
    }
}
