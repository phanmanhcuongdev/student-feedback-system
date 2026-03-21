import SurveysDetailPage from "./features/surveys/pages/SurveyDetailPage.tsx";
import SurveysListPage from "./features/surveys/pages/SurveysPage.tsx";
import { Routes, Route } from "react-router-dom";

function App() {
  return (
      <Routes>
          <Route path="/surveys" element={<SurveysListPage />} />
          <Route path="/surveys/:id" element={<SurveysDetailPage />} />
      </Routes>
  );
}

export default App