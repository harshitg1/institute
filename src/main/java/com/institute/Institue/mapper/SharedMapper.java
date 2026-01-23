package com.institute.Institue.mapper;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
        componentModel = "spring",

        unmappedTargetPolicy = ReportingPolicy.IGNORE,

        injectionStrategy = InjectionStrategy.CONSTRUCTOR,

        collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY
)
public interface SharedMapper {
}
