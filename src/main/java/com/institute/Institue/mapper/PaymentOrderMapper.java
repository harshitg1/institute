package com.institute.Institue.mapper;

import com.institute.Institue.dto.PaymentOrderResponse;
import com.institute.Institue.model.PaymentOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = SharedMapper.class)
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder, PaymentOrderResponse> {

    @Override
    @Mapping(target = "orderId", expression = "java(order.getId() != null ? order.getId().toString() : null)")
    @Mapping(target = "studentName", expression = "java(resolveStudentName(order))")
    @Mapping(target = "courseId", expression = "java(order.getCourse() != null && order.getCourse().getId() != null ? order.getCourse().getId().toString() : null)")
    @Mapping(target = "courseTitle", expression = "java(order.getCourse() != null ? order.getCourse().getTitle() : null)")
    @Mapping(target = "studentId", expression = "java(order.getStudent() != null && order.getStudent().getId() != null ? order.getStudent().getId().toString() : null)")
    @Mapping(target = "provider", expression = "java(order.getProvider() != null ? order.getProvider().name() : null)")
    @Mapping(target = "status", expression = "java(order.getStatus() != null ? order.getStatus().name() : null)")
    @Mapping(target = "createdAt", source = "createdAt")
    PaymentOrderResponse toDto(PaymentOrder order);

    @Override
    @Mapping(target = "id", expression = "java(dto.getOrderId() != null ? java.util.UUID.fromString(dto.getOrderId()) : null)")
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentOrder toEntity(PaymentOrderResponse dto);

    default String resolveStudentName(PaymentOrder order) {
        if (order.getStudent() == null) {
            return null;
        }
        String fn = order.getStudent().getFirstName();
        String ln = order.getStudent().getLastName();
        String name = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
        return name.isEmpty() ? order.getStudent().getEmail() : name;
    }
}
