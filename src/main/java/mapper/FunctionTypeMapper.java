package mapper;

import dto.function.FunctionTypeDto;
import entity.FunctionTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FunctionTypeMapper {
    FunctionTypeMapper INSTANCE = Mappers.getMapper(FunctionTypeMapper.class);

    FunctionTypeDto toDto(FunctionTypeEntity entity);
}
