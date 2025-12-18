import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './Home';
import TagSuggestion from './components/TagSuggestion';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/tag-suggestion" element={<TagSuggestion />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
