Run:
* `./gradlew runServer` to spin up a test syphon server which produces a 225x225 image with a red square in the middle
* `./gradlew runClientWithExampleMesh` to spin up a client which reads from the server and applies the transformation from Paul Bourke's sample mesh: http://paulbourke.net/dataformats/meshwarp/xyuv.txt
* `./gradlew runClientWithTestMesh` to spin up a client which reads from the server and applies the identity transformation defined in test-mesh.txt

Important files:
* MeshWarp.java for the code
* test-mesh.txt / example-mesh.txt for the warp meshes
* build.gradle defines gradle tasks to run the server and to run the client with a given mesh.
