IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'content_original') IS NULL
BEGIN
    ALTER TABLE [dbo].[Feedback]
    ADD [content_original] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'content_translated') IS NULL
BEGIN
    ALTER TABLE [dbo].[Feedback]
    ADD [content_translated] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'source_lang') IS NULL
BEGIN
    ALTER TABLE [dbo].[Feedback]
    ADD [source_lang] VARCHAR(10) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'is_auto_translated') IS NULL
BEGIN
    ALTER TABLE [dbo].[Feedback]
    ADD [is_auto_translated] BIT NOT NULL
        CONSTRAINT [DF_Feedback_IsAutoTranslated] DEFAULT ((0));
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'content_original') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [content_original] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'content_translated') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [content_translated] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'source_lang') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [source_lang] VARCHAR(10) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'is_auto_translated') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [is_auto_translated] BIT NOT NULL
        CONSTRAINT [DF_SurveyQuestion_IsAutoTranslated] DEFAULT ((0));
END
GO
