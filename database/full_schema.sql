USE [SURVEY_SYSTEM_DEV];
GO

CREATE TABLE [dbo].[User] (
    [user_id] INT IDENTITY(1,1) NOT NULL,
    [email] NVARCHAR(255) NOT NULL,
    [pass_word] NVARCHAR(255) NOT NULL,
    [role] NVARCHAR(20) NOT NULL,
    [verify] BIT NOT NULL,
    CONSTRAINT [PK_User] PRIMARY KEY ([user_id]),
    CONSTRAINT [UQ_User_Email] UNIQUE ([email]),
    CONSTRAINT [CHK_User_Role] CHECK ([role] IN ('ADMIN', 'STUDENT', 'LECTURER'))
);
GO

CREATE TABLE [dbo].[Department] (
    [dept_id] INT IDENTITY(1,1) NOT NULL,
    [name] NVARCHAR(255) NOT NULL,
    CONSTRAINT [PK_Department] PRIMARY KEY ([dept_id]),
    CONSTRAINT [UQ_Department_Name] UNIQUE ([name])
);
GO

CREATE TABLE [dbo].[Admin] (
    [user_id] INT NOT NULL,
    [name] NVARCHAR(255) NOT NULL,
    CONSTRAINT [PK_Admin] PRIMARY KEY ([user_id]),
    CONSTRAINT [FK_Admin_User] FOREIGN KEY ([user_id]) REFERENCES [dbo].[User]([user_id])
);
GO

CREATE TABLE [dbo].[Lecturer] (
    [user_id] INT NOT NULL,
    [name] NVARCHAR(255) NOT NULL,
    [lecturer_code] NVARCHAR(50) NOT NULL,
    [dept_id] INT NOT NULL,
    CONSTRAINT [PK_Lecturer] PRIMARY KEY ([user_id]),
    CONSTRAINT [UQ_Lecturer_Code] UNIQUE ([lecturer_code]),
    CONSTRAINT [FK_Lecturer_User] FOREIGN KEY ([user_id]) REFERENCES [dbo].[User]([user_id]),
    CONSTRAINT [FK_Lecturer_Department] FOREIGN KEY ([dept_id]) REFERENCES [dbo].[Department]([dept_id])
);
GO

CREATE TABLE [dbo].[Student] (
    [user_id] INT NOT NULL,
    [name] NVARCHAR(255) NOT NULL,
    [student_code] NVARCHAR(50) NOT NULL,
    [dept_id] INT NOT NULL,
    [status] NVARCHAR(50) NOT NULL CONSTRAINT [DF_Student_Status] DEFAULT ('ACTIVE'),
    [student_card_img] NVARCHAR(500) NULL,
    [national_id_img] NVARCHAR(500) NULL,
    [review_reason] NVARCHAR(255) NULL,
    [review_notes] NVARCHAR(MAX) NULL,
    [reviewed_by_user_id] INT NULL,
    [reviewed_at] DATETIME NULL,
    [resubmission_count] INT NOT NULL CONSTRAINT [DF_Student_ResubmissionCount] DEFAULT ((0)),
    CONSTRAINT [PK_Student] PRIMARY KEY ([user_id]),
    CONSTRAINT [UQ_Student_Code] UNIQUE ([student_code]),
    CONSTRAINT [FK_Student_User] FOREIGN KEY ([user_id]) REFERENCES [dbo].[User]([user_id]),
    CONSTRAINT [FK_Student_Department] FOREIGN KEY ([dept_id]) REFERENCES [dbo].[Department]([dept_id]),
    CONSTRAINT [FK_Student_ReviewedByUser] FOREIGN KEY ([reviewed_by_user_id]) REFERENCES [dbo].[User]([user_id]),
    CONSTRAINT [CHK_Student_Status] CHECK ([status] IN ('ACTIVE', 'PENDING', 'EMAIL_VERIFIED', 'EMAIL_UNVERIFIED', 'REJECTED'))
);
GO

CREATE TABLE [dbo].[student_token] (
    [id] INT IDENTITY(1,1) NOT NULL,
    [student_id] INT NOT NULL,
    [token] NVARCHAR(MAX) NULL,
    [expired_at] DATETIME NULL,
    [created_at] DATETIME NULL CONSTRAINT [DF_StudentToken_CreatedAt] DEFAULT (GETDATE()),
    [delete_flg] INT NULL CONSTRAINT [DF_StudentToken_DeleteFlg] DEFAULT ((0)),
    CONSTRAINT [PK_student_token] PRIMARY KEY ([id]),
    CONSTRAINT [FK_student_token_student] FOREIGN KEY ([student_id]) REFERENCES [dbo].[Student]([user_id])
);
GO

CREATE TABLE [dbo].[Password_Reset_Token] (
    [id] INT IDENTITY(1,1) NOT NULL,
    [user_id] INT NOT NULL,
    [token_hash] NVARCHAR(128) NOT NULL,
    [expired_at] DATETIME NOT NULL,
    [created_at] DATETIME NOT NULL,
    [used_at] DATETIME NULL,
    CONSTRAINT [PK_Password_Reset_Token] PRIMARY KEY ([id]),
    CONSTRAINT [FK_PasswordResetToken_User] FOREIGN KEY ([user_id]) REFERENCES [dbo].[User]([user_id])
);
GO

CREATE TABLE [dbo].[Survey] (
    [survey_id] INT IDENTITY(1,1) NOT NULL,
    [title] NVARCHAR(255) NULL,
    [description] NVARCHAR(MAX) NULL,
    [start_date] DATETIME NULL,
    [end_date] DATETIME NULL,
    [lifecycle_state] NVARCHAR(20) NOT NULL CONSTRAINT [DF_Survey_LifecycleState] DEFAULT ('DRAFT'),
    [hidden] BIT NOT NULL CONSTRAINT [DF_Survey_Hidden] DEFAULT ((0)),
    [created_by] INT NOT NULL,
    CONSTRAINT [PK_Survey] PRIMARY KEY ([survey_id]),
    CONSTRAINT [FK_Survey_Admin] FOREIGN KEY ([created_by]) REFERENCES [dbo].[Admin]([user_id]),
    CONSTRAINT [CHK_Survey_LifecycleState] CHECK ([lifecycle_state] IN ('DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED'))
);
GO

CREATE TABLE [dbo].[Question_Bank] (
    [question_bank_id] INT IDENTITY(1,1) NOT NULL,
    [content] NVARCHAR(MAX) NOT NULL,
    [type] NVARCHAR(20) NOT NULL,
    [category] NVARCHAR(120) NULL,
    [active] BIT NOT NULL CONSTRAINT [DF_QuestionBank_Active] DEFAULT ((1)),
    [created_at] DATETIME NOT NULL CONSTRAINT [DF_QuestionBank_CreatedAt] DEFAULT (GETDATE()),
    [updated_at] DATETIME NULL,
    CONSTRAINT [PK_Question_Bank] PRIMARY KEY ([question_bank_id]),
    CONSTRAINT [CHK_QuestionBank_Type] CHECK ([type] IN ('RATING', 'TEXT'))
);
GO

CREATE TABLE [dbo].[Survey_Template] (
    [template_id] INT IDENTITY(1,1) NOT NULL,
    [name] NVARCHAR(255) NOT NULL,
    [description] NVARCHAR(MAX) NULL,
    [suggested_title] NVARCHAR(255) NULL,
    [suggested_survey_description] NVARCHAR(MAX) NULL,
    [recipient_scope] NVARCHAR(30) NOT NULL CONSTRAINT [DF_SurveyTemplate_RecipientScope] DEFAULT ('ALL_STUDENTS'),
    [recipient_department_id] INT NULL,
    [active] BIT NOT NULL CONSTRAINT [DF_SurveyTemplate_Active] DEFAULT ((1)),
    [created_at] DATETIME NOT NULL CONSTRAINT [DF_SurveyTemplate_CreatedAt] DEFAULT (GETDATE()),
    [updated_at] DATETIME NULL,
    CONSTRAINT [PK_Survey_Template] PRIMARY KEY ([template_id]),
    CONSTRAINT [CHK_SurveyTemplate_RecipientScope] CHECK ([recipient_scope] IN ('ALL_STUDENTS', 'DEPARTMENT')),
    CONSTRAINT [FK_SurveyTemplate_Department] FOREIGN KEY ([recipient_department_id]) REFERENCES [dbo].[Department]([dept_id])
);
GO

CREATE TABLE [dbo].[Survey_Template_Question] (
    [template_question_id] INT IDENTITY(1,1) NOT NULL,
    [template_id] INT NOT NULL,
    [question_bank_id] INT NULL,
    [content] NVARCHAR(MAX) NOT NULL,
    [type] NVARCHAR(20) NOT NULL,
    [display_order] INT NOT NULL,
    CONSTRAINT [PK_Survey_Template_Question] PRIMARY KEY ([template_question_id]),
    CONSTRAINT [FK_SurveyTemplateQuestion_Template] FOREIGN KEY ([template_id]) REFERENCES [dbo].[Survey_Template]([template_id]),
    CONSTRAINT [FK_SurveyTemplateQuestion_QuestionBank] FOREIGN KEY ([question_bank_id]) REFERENCES [dbo].[Question_Bank]([question_bank_id]),
    CONSTRAINT [CHK_SurveyTemplateQuestion_Type] CHECK ([type] IN ('RATING', 'TEXT'))
);
GO

CREATE TABLE [dbo].[Question] (
    [question_id] INT IDENTITY(1,1) NOT NULL,
    [survey_id] INT NOT NULL,
    [question_bank_id] INT NULL,
    [content] NVARCHAR(MAX) NULL,
    [type] NVARCHAR(20) NULL,
    CONSTRAINT [PK_Question] PRIMARY KEY ([question_id]),
    CONSTRAINT [FK_Question_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id]),
    CONSTRAINT [FK_Question_QuestionBank] FOREIGN KEY ([question_bank_id]) REFERENCES [dbo].[Question_Bank]([question_bank_id]),
    CONSTRAINT [CHK_Question_Content] CHECK ([type] = 'RATING' OR ([type] = 'TEXT' AND [content] IS NOT NULL))
);
GO

CREATE TABLE [dbo].[Survey_Assignment] (
    [id] INT IDENTITY(1,1) NOT NULL,
    [survey_id] INT NOT NULL,
    [evaluator_type] NVARCHAR(50) NOT NULL,
    [evaluator_value] INT NULL,
    [subject_type] NVARCHAR(50) NOT NULL,
    [subject_value] INT NULL,
    CONSTRAINT [PK_SurveyAssignment] PRIMARY KEY ([id]),
    CONSTRAINT [FK_SurveyAssignment_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id])
);
GO

CREATE TABLE [dbo].[Survey_Recipient] (
    [recipient_id] INT IDENTITY(1,1) NOT NULL,
    [survey_id] INT NOT NULL,
    [student_id] INT NOT NULL,
    [assigned_at] DATETIME NOT NULL CONSTRAINT [DF_SurveyRecipient_AssignedAt] DEFAULT (GETDATE()),
    [opened_at] DATETIME NULL,
    [submitted_at] DATETIME NULL,
    CONSTRAINT [PK_Survey_Recipient] PRIMARY KEY ([recipient_id]),
    CONSTRAINT [UQ_SurveyRecipient_SurveyStudent] UNIQUE ([survey_id], [student_id]),
    CONSTRAINT [FK_SurveyRecipient_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id]),
    CONSTRAINT [FK_SurveyRecipient_Student] FOREIGN KEY ([student_id]) REFERENCES [dbo].[Student]([user_id])
);
GO

CREATE TABLE [dbo].[Survey_Response] (
    [response_id] INT IDENTITY(1,1) NOT NULL,
    [student_id] INT NULL,
    [survey_id] INT NOT NULL,
    [lecturer_id] INT NULL,
    [submitted_at] DATETIME NOT NULL,
    CONSTRAINT [PK_Survey_Response] PRIMARY KEY ([response_id]),
    CONSTRAINT [FK_SurveyResponse_Student] FOREIGN KEY ([student_id]) REFERENCES [dbo].[Student]([user_id]),
    CONSTRAINT [FK_SurveyResponse_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id]),
    CONSTRAINT [FK_SurveyResponse_Lecturer] FOREIGN KEY ([lecturer_id]) REFERENCES [dbo].[Lecturer]([user_id]),
    CONSTRAINT [CK_Response_StudentOrLecturer] CHECK (
        ([student_id] IS NOT NULL AND [lecturer_id] IS NULL)
        OR ([student_id] IS NULL AND [lecturer_id] IS NOT NULL)
    )
);
GO

CREATE TABLE [dbo].[Response_Detail] (
    [id] INT IDENTITY(1,1) NOT NULL,
    [response_id] INT NOT NULL,
    [question_id] INT NOT NULL,
    [rating] INT NULL,
    [comment] NVARCHAR(MAX) NULL,
    CONSTRAINT [PK_Response_Detail] PRIMARY KEY ([id]),
    CONSTRAINT [FK_ResponseDetail_Response] FOREIGN KEY ([response_id]) REFERENCES [dbo].[Survey_Response]([response_id]),
    CONSTRAINT [FK_ResponseDetail_Question] FOREIGN KEY ([question_id]) REFERENCES [dbo].[Question]([question_id]),
    CONSTRAINT [CK_ResponseDetail_RatingOrComment] CHECK ([rating] IS NOT NULL OR [comment] IS NOT NULL)
);
GO

CREATE TABLE [dbo].[Feedback] (
    [feedback_id] INT IDENTITY(1,1) NOT NULL,
    [student_id] INT NOT NULL,
    [title] NVARCHAR(255) NOT NULL,
    [content] NVARCHAR(MAX) NOT NULL,
    [created_at] DATETIME NOT NULL,
    CONSTRAINT [PK_Feedback] PRIMARY KEY ([feedback_id]),
    CONSTRAINT [FK_Feedback_Student] FOREIGN KEY ([student_id]) REFERENCES [dbo].[Student]([user_id])
);
GO

CREATE TABLE [dbo].[Feedback_Response] (
    [response_id] INT IDENTITY(1,1) NOT NULL,
    [feedback_id] INT NOT NULL,
    [responder_user_id] INT NOT NULL,
    [content] NVARCHAR(MAX) NOT NULL,
    [created_at] DATETIME NOT NULL,
    CONSTRAINT [PK_Feedback_Response] PRIMARY KEY ([response_id]),
    CONSTRAINT [FK_FeedbackResponse_Feedback] FOREIGN KEY ([feedback_id]) REFERENCES [dbo].[Feedback]([feedback_id]),
    CONSTRAINT [FK_FeedbackResponse_User] FOREIGN KEY ([responder_user_id]) REFERENCES [dbo].[User]([user_id])
);
GO

CREATE TABLE [dbo].[Audit_Log] (
    [audit_id] INT IDENTITY(1,1) NOT NULL,
    [actor_user_id] INT NOT NULL,
    [action_type] NVARCHAR(50) NOT NULL,
    [target_type] NVARCHAR(30) NOT NULL,
    [target_id] INT NOT NULL,
    [summary] NVARCHAR(255) NOT NULL,
    [details] NVARCHAR(MAX) NULL,
    [old_state] NVARCHAR(255) NULL,
    [new_state] NVARCHAR(255) NULL,
    [created_at] DATETIME NOT NULL CONSTRAINT [DF_AuditLog_CreatedAt] DEFAULT (GETDATE()),
    CONSTRAINT [PK_Audit_Log] PRIMARY KEY ([audit_id]),
    CONSTRAINT [FK_AuditLog_ActorUser] FOREIGN KEY ([actor_user_id]) REFERENCES [dbo].[User]([user_id])
);
GO

CREATE TABLE [dbo].[Notification] (
    [noti_id] INT IDENTITY(1,1) NOT NULL,
    [content] NVARCHAR(MAX) NOT NULL,
    CONSTRAINT [PK_Notification] PRIMARY KEY ([noti_id])
);
GO

CREATE TABLE [dbo].[Notification_User] (
    [id] INT IDENTITY(1,1) NOT NULL,
    [noti_id] INT NOT NULL,
    [user_id] INT NOT NULL,
    CONSTRAINT [PK_Notification_User] PRIMARY KEY ([id]),
    CONSTRAINT [FK_NotificationUser_Notification] FOREIGN KEY ([noti_id]) REFERENCES [dbo].[Notification]([noti_id]),
    CONSTRAINT [FK_NotificationUser_User] FOREIGN KEY ([user_id]) REFERENCES [dbo].[User]([user_id])
);
GO
