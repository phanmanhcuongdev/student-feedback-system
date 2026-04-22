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
    ADD [source_lang] VARCHAR(10) NULL
        CONSTRAINT [DF_Feedback_SourceLang] DEFAULT ('en');
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'source_lang') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
       INNER JOIN sys.tables t ON t.object_id = c.object_id
       INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
       WHERE s.name = 'dbo'
         AND t.name = 'Feedback'
         AND c.name = 'source_lang'
   )
BEGIN
    UPDATE [dbo].[Feedback]
    SET [source_lang] = 'en'
    WHERE [source_lang] IS NULL;

    ALTER TABLE [dbo].[Feedback]
    ADD CONSTRAINT [DF_Feedback_SourceLang] DEFAULT ('en') FOR [source_lang];
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

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'is_auto_translated') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
       INNER JOIN sys.tables t ON t.object_id = c.object_id
       INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
       WHERE s.name = 'dbo'
         AND t.name = 'Feedback'
         AND c.name = 'is_auto_translated'
   )
BEGIN
    UPDATE [dbo].[Feedback]
    SET [is_auto_translated] = 0
    WHERE [is_auto_translated] IS NULL;

    ALTER TABLE [dbo].[Feedback]
    ADD CONSTRAINT [DF_Feedback_IsAutoTranslated] DEFAULT ((0)) FOR [is_auto_translated];
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Feedback_Translation_Status'
         AND [object_id] = OBJECT_ID(N'[dbo].[Feedback]')
   )
BEGIN
    CREATE INDEX [IX_Feedback_Translation_Status]
    ON [dbo].[Feedback] ([is_auto_translated], [source_lang]);
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
    ADD [source_lang] VARCHAR(10) NULL
        CONSTRAINT [DF_SurveyQuestion_SourceLang] DEFAULT ('en');
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'source_lang') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
       INNER JOIN sys.tables t ON t.object_id = c.object_id
       INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
       WHERE s.name = 'dbo'
         AND t.name = 'Survey_Question'
         AND c.name = 'source_lang'
   )
BEGIN
    UPDATE [dbo].[Survey_Question]
    SET [source_lang] = 'en'
    WHERE [source_lang] IS NULL;

    ALTER TABLE [dbo].[Survey_Question]
    ADD CONSTRAINT [DF_SurveyQuestion_SourceLang] DEFAULT ('en') FOR [source_lang];
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

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'is_auto_translated') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
       INNER JOIN sys.tables t ON t.object_id = c.object_id
       INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
       WHERE s.name = 'dbo'
         AND t.name = 'Survey_Question'
         AND c.name = 'is_auto_translated'
   )
BEGIN
    UPDATE [dbo].[Survey_Question]
    SET [is_auto_translated] = 0
    WHERE [is_auto_translated] IS NULL;

    ALTER TABLE [dbo].[Survey_Question]
    ADD CONSTRAINT [DF_SurveyQuestion_IsAutoTranslated] DEFAULT ((0)) FOR [is_auto_translated];
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_SurveyQuestion_Translation_Status'
         AND [object_id] = OBJECT_ID(N'[dbo].[Survey_Question]')
   )
BEGIN
    CREATE INDEX [IX_SurveyQuestion_Translation_Status]
    ON [dbo].[Survey_Question] ([is_auto_translated], [source_lang]);
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'content_translated') IS NULL
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD [content_translated] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'source_lang') IS NULL
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD [source_lang] VARCHAR(10) NULL
        CONSTRAINT [DF_Question_SourceLang] DEFAULT ('en');
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'source_lang') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
       INNER JOIN sys.tables t ON t.object_id = c.object_id
       INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
       WHERE s.name = 'dbo'
         AND t.name = 'Question'
         AND c.name = 'source_lang'
   )
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD CONSTRAINT [DF_Question_SourceLang] DEFAULT ('en') FOR [source_lang];
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'is_auto_translated') IS NULL
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD [is_auto_translated] BIT NOT NULL
        CONSTRAINT [DF_Question_IsAutoTranslated] DEFAULT ((0));
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'is_auto_translated') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c ON c.default_object_id = dc.object_id
       INNER JOIN sys.tables t ON t.object_id = c.object_id
       INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
       WHERE s.name = 'dbo'
         AND t.name = 'Question'
         AND c.name = 'is_auto_translated'
   )
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD CONSTRAINT [DF_Question_IsAutoTranslated] DEFAULT ((0)) FOR [is_auto_translated];
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Question_Translation_Status'
         AND [object_id] = OBJECT_ID(N'[dbo].[Question]')
   )
BEGIN
    CREATE INDEX [IX_Question_Translation_Status]
    ON [dbo].[Question] ([is_auto_translated], [source_lang]);
END
GO
