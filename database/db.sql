USE [SURVEY_SYSTEM_DEV]
GO
/****** Object:  Table [dbo].[Admin]    Script Date: 4/13/2026 11:08:43 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Admin](
    [user_id] [int] NOT NULL,
    [name] [nvarchar](255) NOT NULL,
    PRIMARY KEY CLUSTERED
(
[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Department]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Department](
    [dept_id] [int] IDENTITY(1,1) NOT NULL,
    [name] [nvarchar](255) NOT NULL,
    PRIMARY KEY CLUSTERED
(
[dept_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
    UNIQUE NONCLUSTERED
(
[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Notification]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Notification](
    [noti_id] [int] IDENTITY(1,1) NOT NULL,
    [content] [nvarchar](max) NOT NULL,
    PRIMARY KEY CLUSTERED
(
[noti_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Notification_User]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Notification_User](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [noti_id] [int] NOT NULL,
    [user_id] [int] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Question]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Question](
    [question_id] [int] IDENTITY(1,1) NOT NULL,
    [survey_id] [int] NOT NULL,
    [content] [nvarchar](max) NULL,
    [type] [nvarchar](20) NULL,
    PRIMARY KEY CLUSTERED
(
[question_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Response_Detail]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Response_Detail](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [response_id] [int] NOT NULL,
    [question_id] [int] NOT NULL,
    [rating] [int] NULL,
    [comment] [nvarchar](max) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Feedback]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Feedback](
    [feedback_id] [int] IDENTITY(1,1) NOT NULL,
    [student_id] [int] NOT NULL,
    [title] [nvarchar](255) NOT NULL,
    [content] [nvarchar](max) NOT NULL,
    [created_at] [datetime] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[feedback_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Feedback_Response]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Feedback_Response](
    [response_id] [int] IDENTITY(1,1) NOT NULL,
    [feedback_id] [int] NOT NULL,
    [responder_user_id] [int] NOT NULL,
    [content] [nvarchar](max) NOT NULL,
    [created_at] [datetime] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[response_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Student]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Student](
    [user_id] [int] NOT NULL,
    [name] [nvarchar](255) NOT NULL,
    [student_code] [nvarchar](50) NOT NULL,
    [dept_id] [int] NOT NULL,
    [status] [nvarchar](50) NULL,
    [student_card_img] [nvarchar](500) NULL,
    [national_id_img] [nvarchar](500) NULL,
    PRIMARY KEY CLUSTERED
(
[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
    UNIQUE NONCLUSTERED
(
[student_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[student_token]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[student_token](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [student_id] [int] NOT NULL,
    [token] [nvarchar](max) NULL,
    [expired_at] [datetime] NULL,
    [created_at] [datetime] NULL,
    [delete_flg] [int] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Password_Reset_Token]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Password_Reset_Token](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [user_id] [int] NOT NULL,
    [token_hash] [nvarchar](128) NOT NULL,
    [expired_at] [datetime] NOT NULL,
    [created_at] [datetime] NOT NULL,
    [used_at] [datetime] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Survey]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Survey](
    [survey_id] [int] IDENTITY(1,1) NOT NULL,
    [title] [nvarchar](255) NULL,
    [description] [nvarchar](max) NULL,
    [start_date] [datetime] NULL,
    [end_date] [datetime] NULL,
    [hidden] [bit] NOT NULL,
    [created_by] [int] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[survey_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Survey_Assignment]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Survey_Assignment](
    [survey_id] [int] NOT NULL,
    [evaluator_type] [nvarchar](50) NOT NULL,
    [evaluator_value] [int] NULL,
    [subject_type] [nvarchar](50) NOT NULL,
    [subject_value] [int] NULL,
    [id] [int] IDENTITY(1,1) NOT NULL,
    CONSTRAINT [PK_SurveyAssignment] PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Survey_Response]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Survey_Response](
    [response_id] [int] IDENTITY(1,1) NOT NULL,
    [student_id] [int] NULL,
    [survey_id] [int] NOT NULL,
    [teacher_id] [int] NULL,
    [submitted_at] [datetime] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[response_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[Teacher]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[Teacher](
    [user_id] [int] NOT NULL,
    [name] [nvarchar](255) NOT NULL,
    [teacher_code] [nvarchar](50) NOT NULL,
    [dept_id] [int] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
    UNIQUE NONCLUSTERED
(
[teacher_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
    GO
/****** Object:  Table [dbo].[User]    Script Date: 4/13/2026 11:08:43 AM ******/
    SET ANSI_NULLS ON
    GO
    SET QUOTED_IDENTIFIER ON
    GO
CREATE TABLE [dbo].[User](
    [user_id] [int] IDENTITY(1,1) NOT NULL,
    [email] [nvarchar](255) NULL,
    [pass_word] [nvarchar](255) NOT NULL,
    [role] [nvarchar](20) NULL,
    [verify] [bit] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
    UNIQUE NONCLUSTERED
(
[email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]
    GO
ALTER TABLE [dbo].[Student] ADD  DEFAULT ('ACTIVE') FOR [status]
    GO
ALTER TABLE [dbo].[student_token] ADD  DEFAULT (getdate()) FOR [created_at]
    GO
ALTER TABLE [dbo].[student_token] ADD  DEFAULT ((0)) FOR [delete_flg]
    GO
ALTER TABLE [dbo].[Survey] ADD DEFAULT ((0)) FOR [hidden]
    GO
ALTER TABLE [dbo].[Admin]  WITH CHECK ADD FOREIGN KEY([user_id])
    REFERENCES [dbo].[User] ([user_id])
    GO
ALTER TABLE [dbo].[Notification_User]  WITH CHECK ADD FOREIGN KEY([noti_id])
    REFERENCES [dbo].[Notification] ([noti_id])
    GO
ALTER TABLE [dbo].[Notification_User]  WITH CHECK ADD FOREIGN KEY([user_id])
    REFERENCES [dbo].[User] ([user_id])
    GO
ALTER TABLE [dbo].[Question]  WITH CHECK ADD FOREIGN KEY([survey_id])
    REFERENCES [dbo].[Survey] ([survey_id])
    GO
ALTER TABLE [dbo].[Response_Detail]  WITH CHECK ADD FOREIGN KEY([question_id])
    REFERENCES [dbo].[Question] ([question_id])
    GO
ALTER TABLE [dbo].[Response_Detail]  WITH CHECK ADD FOREIGN KEY([response_id])
    REFERENCES [dbo].[Survey_Response] ([response_id])
    GO
ALTER TABLE [dbo].[Feedback]  WITH CHECK ADD FOREIGN KEY([student_id])
    REFERENCES [dbo].[Student] ([user_id])
    GO
ALTER TABLE [dbo].[Feedback_Response]  WITH CHECK ADD FOREIGN KEY([feedback_id])
    REFERENCES [dbo].[Feedback] ([feedback_id])
    GO
ALTER TABLE [dbo].[Feedback_Response]  WITH CHECK ADD FOREIGN KEY([responder_user_id])
    REFERENCES [dbo].[User] ([user_id])
    GO
ALTER TABLE [dbo].[Student]  WITH CHECK ADD FOREIGN KEY([dept_id])
    REFERENCES [dbo].[Department] ([dept_id])
    GO
ALTER TABLE [dbo].[Student]  WITH CHECK ADD FOREIGN KEY([user_id])
    REFERENCES [dbo].[User] ([user_id])
    GO
ALTER TABLE [dbo].[student_token]  WITH CHECK ADD  CONSTRAINT [FK_student_token_student] FOREIGN KEY([student_id])
    REFERENCES [dbo].[Student] ([user_id])
    GO
ALTER TABLE [dbo].[student_token] CHECK CONSTRAINT [FK_student_token_student]
    GO
ALTER TABLE [dbo].[Password_Reset_Token]  WITH CHECK ADD  CONSTRAINT [FK_PasswordResetToken_User] FOREIGN KEY([user_id])
    REFERENCES [dbo].[User] ([user_id])
    GO
ALTER TABLE [dbo].[Password_Reset_Token] CHECK CONSTRAINT [FK_PasswordResetToken_User]
    GO
ALTER TABLE [dbo].[Survey]  WITH CHECK ADD FOREIGN KEY([created_by])
    REFERENCES [dbo].[Admin] ([user_id])
    GO
ALTER TABLE [dbo].[Survey_Assignment]  WITH CHECK ADD FOREIGN KEY([survey_id])
    REFERENCES [dbo].[Survey] ([survey_id])
    GO
ALTER TABLE [dbo].[Survey_Response]  WITH CHECK ADD FOREIGN KEY([student_id])
    REFERENCES [dbo].[Student] ([user_id])
    GO
ALTER TABLE [dbo].[Survey_Response]  WITH CHECK ADD FOREIGN KEY([survey_id])
    REFERENCES [dbo].[Survey] ([survey_id])
    GO
ALTER TABLE [dbo].[Survey_Response]  WITH CHECK ADD FOREIGN KEY([teacher_id])
    REFERENCES [dbo].[Teacher] ([user_id])
    GO
ALTER TABLE [dbo].[Teacher]  WITH CHECK ADD FOREIGN KEY([dept_id])
    REFERENCES [dbo].[Department] ([dept_id])
    GO
ALTER TABLE [dbo].[Teacher]  WITH CHECK ADD FOREIGN KEY([user_id])
    REFERENCES [dbo].[User] ([user_id])
    GO
ALTER TABLE [dbo].[Question]  WITH CHECK ADD  CONSTRAINT [CK_Question_Content] CHECK  (([type]='RATING' OR [type]='TEXT' AND [content] IS NOT NULL))
    GO
ALTER TABLE [dbo].[Question] CHECK CONSTRAINT [CK_Question_Content]
    GO
ALTER TABLE [dbo].[Response_Detail]  WITH CHECK ADD  CONSTRAINT [CK_ResponseDetail_RatingOrComment] CHECK  (([rating] IS NOT NULL OR [comment] IS NOT NULL))
    GO
ALTER TABLE [dbo].[Response_Detail] CHECK CONSTRAINT [CK_ResponseDetail_RatingOrComment]
    GO
ALTER TABLE [dbo].[Student]  WITH CHECK ADD  CONSTRAINT [CHK_Student_Status] CHECK  (([status]='REJECTED' OR [status]='PENDING' OR [status]='EMAIL_VERIFIED' OR [status]='EMAIL_UNVERIFIED' OR [status]='ACTIVE'))
    GO
ALTER TABLE [dbo].[Student] CHECK CONSTRAINT [CHK_Student_Status]
    GO
ALTER TABLE [dbo].[Survey_Response]  WITH CHECK ADD  CONSTRAINT [CK_Response_StudentOrTeacher] CHECK  (([student_id] IS NOT NULL AND [teacher_id] IS NULL OR [student_id] IS NULL AND [teacher_id] IS NOT NULL))
    GO
ALTER TABLE [dbo].[Survey_Response] CHECK CONSTRAINT [CK_Response_StudentOrTeacher]
    GO
ALTER TABLE [dbo].[User]  WITH CHECK ADD CHECK  (([role]='TEACHER' OR [role]='STUDENT' OR [role]='ADMIN'))
    GO
