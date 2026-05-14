CREATE TABLE [dbo].[Survey_AI_Summary_Theme_Embedding] (
    [embedding_id] INT IDENTITY(1,1) NOT NULL,
    [summary_id] INT NOT NULL,
    [survey_id] INT NOT NULL,
    [theme_type] NVARCHAR(30) NOT NULL,
    [theme_index] INT NOT NULL,
    [theme_text] NVARCHAR(1000) NOT NULL,
    [embedding_json] NVARCHAR(MAX) NOT NULL,
    [model_name] NVARCHAR(100) NULL,
    [created_at] DATETIME NOT NULL CONSTRAINT [DF_SurveyAISummaryThemeEmbedding_CreatedAt] DEFAULT (GETDATE()),
    CONSTRAINT [PK_Survey_AI_Summary_Theme_Embedding] PRIMARY KEY ([embedding_id]),
    CONSTRAINT [FK_SurveyAIThemeEmbedding_Summary] FOREIGN KEY ([summary_id]) REFERENCES [dbo].[Survey_AI_Summary]([summary_id]),
    CONSTRAINT [FK_SurveyAIThemeEmbedding_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id])
);
GO

CREATE INDEX [IX_SurveyAIThemeEmbedding_SurveySummary]
    ON [dbo].[Survey_AI_Summary_Theme_Embedding] ([survey_id], [summary_id]);
GO
