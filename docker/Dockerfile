FROM ubuntu as builder

RUN apt-get update && \
    apt-get install -y --no-install-recommends git wget maven unzip && \
    rm -rf /var/lib/apt/lists/*

# Install protoc
RUN wget https://github.com/protocolbuffers/protobuf/releases/download/v28.2/protoc-28.2-linux-x86_64.zip && \
    unzip protoc-28.2-linux-x86_64.zip -d /usr/local && \
    rm protoc-28.2-linux-x86_64.zip

# Ensure protoc is available in the PATH
ENV PATH="$PATH:/usr/local/bin"

ARG WIREMOCK_GRPC_EXTENSION_STANDALONE_VERSION=0.8.1
WORKDIR /build
# Download the wiremock grpc extension
RUN mvn dependency:copy -Dartifact=org.wiremock:wiremock-grpc-extension-standalone:$WIREMOCK_GRPC_EXTENSION_STANDALONE_VERSION -DoutputDirectory=extensions/

# Clone googleapi and compile service protos
RUN mkdir grpc
RUN git clone https://github.com/googleapis/googleapis.git
# To support more apis add compilations here:
RUN protoc --proto_path /build/googleapis --include_imports --descriptor_set_out grpc/gcp_secretmanager.dsc google/cloud/secretmanager/v1/service.proto
RUN protoc --proto_path /build/googleapis --include_imports --descriptor_set_out grpc/gcp_kms.dsc google/cloud/kms/v1/service.proto

FROM wiremock/wiremock:3.9.1-1
COPY --from=builder /build/extensions /var/wiremock/extensions
COPY --from=builder /build/grpc /home/wiremock/grpc
