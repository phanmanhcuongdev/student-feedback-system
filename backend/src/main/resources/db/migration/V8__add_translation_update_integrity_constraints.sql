IF OBJECT_ID(N'[dbo].[Feedback]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'content_translated') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'source_lang') IS NOT NULL
   AND COL_LENGTH(N'dbo.Feedback', N'is_auto_translated') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.check_constraints
       WHERE [name] = 'CHK_Feedback_Translation_Update_Integrity'
         AND [parent_object_id] = OBJECT_ID(N'[dbo].[Feedback]')
   )
BEGIN
    ALTER TABLE [dbo].[Feedback] WITH NOCHECK
    ADD CONSTRAINT [CHK_Feedback_Translation_Update_Integrity]
    CHECK (
        [is_auto_translated] = 0
        OR ([content_translated] IS NOT NULL AND [source_lang] IS NOT NULL)
    );
END
GO

IF OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'content_translated') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'source_lang') IS NOT NULL
   AND COL_LENGTH(N'dbo.Survey_Question', N'is_auto_translated') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.check_constraints
       WHERE [name] = 'CHK_SurveyQuestion_Translation_Update_Integrity'
         AND [parent_object_id] = OBJECT_ID(N'[dbo].[Survey_Question]')
   )
BEGIN
    ALTER TABLE [dbo].[Survey_Question] WITH NOCHECK
    ADD CONSTRAINT [CHK_SurveyQuestion_Translation_Update_Integrity]
    CHECK (
        [is_auto_translated] = 0
        OR ([content_translated] IS NOT NULL AND [source_lang] IS NOT NULL)
    );
END
GO

IF OBJECT_ID(N'[dbo].[Question]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'content_translated') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'source_lang') IS NOT NULL
   AND COL_LENGTH(N'dbo.Question', N'is_auto_translated') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.check_constraints
       WHERE [name] = 'CHK_Question_Translation_Update_Integrity'
         AND [parent_object_id] = OBJECT_ID(N'[dbo].[Question]')
   )
BEGIN
    ALTER TABLE [dbo].[Question] WITH NOCHECK
    ADD CONSTRAINT [CHK_Question_Translation_Update_Integrity]
    CHECK (
        [is_auto_translated] = 0
        OR ([content_translated] IS NOT NULL AND [source_lang] IS NOT NULL)
    );
END
GO
