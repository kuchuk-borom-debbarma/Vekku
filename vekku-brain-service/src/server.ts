import express from 'express';
import cors from 'cors';
import bodyParser from 'body-parser';
import { BrainController } from './controllers/BrainController';
import { BrainLogic } from './services/brain-logic/BrainLogic';

import { config } from './config';

const app = express();
const PORT = config.port;

// Middleware
app.use(cors());
app.use(bodyParser.json());

// Routes
// POST because we are sending data (even if suggest is technically a read, we send a body)
app.post('/learn', BrainController.Learn);

app.post('/raw-tags', BrainController.GetRawTags);
app.post('/region-tags', BrainController.GetRegionTags);
app.post('/score-tags', BrainController.ScoreTags);

app.get('/tags', BrainController.GetAllTags);
app.delete('/tags/:id', BrainController.DeleteTag);


// Initialize AI/DB *before* accepting requests
const brain = BrainLogic.getInstance();

brain.initialize().then(() => {
    app.listen(PORT, () => {
        console.log(`🤖 Vekku Brain Service (REST) running on port ${PORT}`);
    });
});