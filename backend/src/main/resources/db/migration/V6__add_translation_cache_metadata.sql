IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'model_info') IS NULL
BEGIN
    ALTER TABLE [dbo].[Feedback]
    ADD [model_info] VARCHAR(100) NULL
        CONSTRAINT [DF_Feedback_ModelInfo] DEFAULT ('default_model');
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'target_lang') IS NULL
BEGIN
    ALTER TABLE [dbo].[Feedback]
    ADD [target_lang] VARCHAR(10) NULL
        CONSTRAINT [DF_Feedback_TargetLang] DEFAULT ('en');
END
GO

IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Feedback_Translation_Cache'
         AND [object_id] = OBJECT_ID(N'[dbo].[Feedback]')
   )
BEGIN
    CREATE INDEX [IX_Feedback_Translation_Cache]
    ON [dbo].[Feedback] ([model_info], [target_lang]);
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'model_info') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [model_info] VARCHAR(100) NULL
        CONSTRAINT [DF_SurveyQuestion_ModelInfo] DEFAULT ('default_model');
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'target_lang') IS NULL
BEGIN
    ALTER TABLE [dbo].[Survey_Question]
    ADD [target_lang] VARCHAR(10) NULL
        CONSTRAINT [DF_SurveyQuestion_TargetLang] DEFAULT ('en');
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_SurveyQuestion_Translation_Cache'
         AND [object_id] = OBJECT_ID(N'[dbo].[Survey_Question]')
   )
BEGIN
    CREATE INDEX [IX_SurveyQuestion_Translation_Cache]
    ON [dbo].[Survey_Question] ([model_info], [target_lang]);
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'model_info') IS NULL
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD [model_info] VARCHAR(100) NULL
        CONSTRAINT [DF_Question_ModelInfo] DEFAULT ('default_model');
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'target_lang') IS NULL
BEGIN
    ALTER TABLE [dbo].[Question]
    ADD [target_lang] VARCHAR(10) NULL
        CONSTRAINT [DF_Question_TargetLang] DEFAULT ('en');
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_Question_Translation_Cache'
         AND [object_id] = OBJECT_ID(N'[dbo].[Question]')
   )
BEGIN
    CREATE INDEX [IX_Question_Translation_Cache]
    ON [dbo].[Question] ([model_info], [target_lang]);
END
GO
