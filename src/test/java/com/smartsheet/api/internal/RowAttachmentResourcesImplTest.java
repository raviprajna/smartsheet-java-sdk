package com.smartsheet.api.internal;

import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.internal.http.DefaultHttpClient;
import com.smartsheet.api.models.Attachment;
import com.smartsheet.api.models.PagedResult;
import com.smartsheet.api.models.PaginationParameters;
import com.smartsheet.api.models.enums.AttachmentParentType;
import com.smartsheet.api.models.enums.AttachmentSubType;
import com.smartsheet.api.models.enums.AttachmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * #[license]
 * Smartsheet SDK for Java
 * %%
 * Copyright (C) 2023 Smartsheet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * %[license]
 */
class RowAttachmentResourcesImplTest extends ResourcesImplBase {

    private  RowAttachmentResourcesImpl rowAttachmentResources;

    @BeforeEach
    public void setUp() throws Exception {
        rowAttachmentResources = new RowAttachmentResourcesImpl(new SmartsheetImpl("http://localhost:9090/1.1/",
                "accessToken", new DefaultHttpClient(), serializer));
    }
    @Test
    void testAttachUrl() throws SmartsheetException, IOException {
        server.setResponseBody(new File("src/test/resources/attachLink.json"));

        Attachment attachment = new Attachment();
        attachment.setUrl("http://www.smartsheet.com/sites/all/themes/blue_sky/logo.png");
        attachment.setAttachmentType(AttachmentType.LINK);
        attachment.setUrlExpiresInMillis(1L);
        attachment.setAttachmentSubType(AttachmentSubType.PDF);

        Attachment newAttachment = rowAttachmentResources.attachUrl(1234L, 3456L, attachment);
        assertThat(newAttachment.getName()).isEqualTo("Search Engine");
        assertThat(newAttachment.getAttachmentType()).isEqualTo(AttachmentType.LINK);
    }

    @Test
    void testGetAttachments() throws SmartsheetException, IOException {
        server.setResponseBody(new File("src/test/resources/listAssociatedAttachments.json"));
        PaginationParameters parameters = new PaginationParameters(false, 1,1);

        PagedResult<Attachment> attachments = rowAttachmentResources.getAttachments(1234L, 456L, parameters);
        assertThat(attachments.getTotalCount()).isEqualTo(2);
        assertThat(attachments.getData().get(0).getId()).isEqualTo(4583173393803140L);
    }

    @Test
    void testAttachFile() throws SmartsheetException, IOException {
        server.setResponseBody(new File("src/test/resources/attachFile.json"));
        File file = new File("src/test/resources/large_sheet.pdf");
        Attachment attachment = rowAttachmentResources.attachFile(1234L, 345L, file,
                "application/pdf");
        assertThat(attachment.getId()).isEqualTo(7265404226692996L);
        assertThat(attachment.getName()).isEqualTo("Testing.PDF");
        assertThat(attachment.getAttachmentType()).isEqualTo(AttachmentType.FILE);
        assertThat(attachment.getMimeType()).isEqualTo("application/pdf");
        assertThat(attachment.getSizeInKb()).isEqualTo(1831L);
        assertThat(attachment.getParentType()).isEqualTo(AttachmentParentType.SHEET);
    }

    @Test
    void testAttachFileAsInputStream() throws SmartsheetException, IOException {
        server.setResponseBody(new File("src/test/resources/attachFile.json"));
        File file = new File("src/test/resources/large_sheet.pdf");
        InputStream inputStream = new FileInputStream(file);
        Attachment attachment = rowAttachmentResources.attachFile(1234L, 345L, inputStream, "application/pdf", file.length(), file.getName());
        assertThat(attachment.getMimeType()).isEqualTo("application/pdf");
        assertThat(attachment.getName()).isEqualTo("Testing.PDF");
        assertThat(attachment.getSizeInKb()).isEqualTo(1831L);
        assertThat(attachment.getAttachmentType()).isEqualTo(AttachmentType.FILE);
    }

    @Test
    void testAttachFileAsInputStreamWrongContentLength() throws IOException {
        server.setResponseBody(new File("src/test/resources/attachFile.json"));
        File file = new File("src/test/resources/large_sheet.pdf");
        InputStream inputStream = new FileInputStream(file);
        assertThatThrownBy(() -> rowAttachmentResources.attachFile(1234L, 345L, inputStream, "application/pdf", file.length() + 5, file.getName()))
                .isInstanceOf(SmartsheetException.class);
    }
}
