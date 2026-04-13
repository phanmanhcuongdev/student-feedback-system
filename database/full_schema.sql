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
    CONSTRAINT [CHK_User_Role] CHECK ([role] IN ('ADMIN', 'STUDENT', 'TEACHER'))
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

CREATE TABLE [dbo].[Teacher] (
    [user_id] INT NOT NULL,
    [name] NVARCHAR(255) NOT NULL,
    [teacher_code] NVARCHAR(50) NOT NULL,
    [dept_id] INT NOT NULL,
    CONSTRAINT [PK_Teacher] PRIMARY KEY ([user_id]),
    CONSTRAINT [UQ_Teacher_Code] UNIQUE ([teacher_code]),
    CONSTRAINT [FK_Teacher_User] FOREIGN KEY ([user_id]) REFERENCES [dbo].[User]([user_id]),
    CONSTRAINT [FK_Teacher_Department] FOREIGN KEY ([dept_id]) REFERENCES [dbo].[Department]([dept_id])
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
    CONSTRAINT [PK_Student] PRIMARY KEY ([user_id]),
    CONSTRAINT [UQ_Student_Code] UNIQUE ([student_code]),
    CONSTRAINT [FK_Student_User] FOREIGN KEY ([user_id]) REFERENCES [dbo].[User]([user_id]),
    CONSTRAINT [FK_Student_Department] FOREIGN KEY ([dept_id]) REFERENCES [dbo].[Department]([dept_id]),
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
    [hidden] BIT NOT NULL CONSTRAINT [DF_Survey_Hidden] DEFAULT ((0)),
    [created_by] INT NOT NULL,
    CONSTRAINT [PK_Survey] PRIMARY KEY ([survey_id]),
    CONSTRAINT [FK_Survey_Admin] FOREIGN KEY ([created_by]) REFERENCES [dbo].[Admin]([user_id])
);
GO

CREATE TABLE [dbo].[Question] (
    [question_id] INT IDENTITY(1,1) NOT NULL,
    [survey_id] INT NOT NULL,
    [content] NVARCHAR(MAX) NULL,
    [type] NVARCHAR(20) NULL,
    CONSTRAINT [PK_Question] PRIMARY KEY ([question_id]),
    CONSTRAINT [FK_Question_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id]),
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

CREATE TABLE [dbo].[Survey_Response] (
    [response_id] INT IDENTITY(1,1) NOT NULL,
    [student_id] INT NULL,
    [survey_id] INT NOT NULL,
    [teacher_id] INT NULL,
    [submitted_at] DATETIME NOT NULL,
    CONSTRAINT [PK_Survey_Response] PRIMARY KEY ([response_id]),
    CONSTRAINT [FK_SurveyResponse_Student] FOREIGN KEY ([student_id]) REFERENCES [dbo].[Student]([user_id]),
    CONSTRAINT [FK_SurveyResponse_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id]),
    CONSTRAINT [FK_SurveyResponse_Teacher] FOREIGN KEY ([teacher_id]) REFERENCES [dbo].[Teacher]([user_id]),
    CONSTRAINT [CK_Response_StudentOrTeacher] CHECK (
        ([student_id] IS NOT NULL AND [teacher_id] IS NULL)
        OR ([student_id] IS NULL AND [teacher_id] IS NOT NULL)
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
