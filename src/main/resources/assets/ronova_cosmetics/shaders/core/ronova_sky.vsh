#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;

out vec3 viewRay;

void main() {
    // Position ist schon in Bildschirmkoordinaten (ein großes Dreieck über den ganzen Bildschirm)
    gl_Position = vec4(Position.xy, 0.0, 1.0);

    // Bildschirmpunkt zurück in eine Blickrichtung umrechnen
    vec4 world = inverse(ProjMat * ModelViewMat) * vec4(Position.xy, 1.0, 1.0);
    viewRay = world.xyz / world.w;
}
