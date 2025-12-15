import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import path from 'path';
import { BrainController } from './controllers/BrainController';

const PROTO_PATH = path.join(__dirname, '../protos/brain.proto');

const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
});

const brainProto = grpc.loadPackageDefinition(packageDefinition) as any;

const server = new grpc.Server();

server.addService(brainProto.brain.BrainService.service, {
    Analyze: BrainController.analyze,
    Learn: BrainController.learn
});

const PORT = 50051;

server.bindAsync(
    `0.0.0.0:${PORT}`,
    grpc.ServerCredentials.createInsecure(),
    (err, port) => {
        if (err) {
            console.error(err);
            return;
        }
        console.log(`🧠 Brain Service running on port ${port}`);
    }
);
