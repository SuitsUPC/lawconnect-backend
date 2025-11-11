package com.qu3dena.lawconnect.backend.cases.interfaces.rest.resources;

public record UploadDocumentResource(
        String filename,
        String fileUrl,
        Long fileSize,
        String fileType
) {
}

