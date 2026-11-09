#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec3 viewRay;

out vec4 fragColor;

const float PI = 3.14159265;

// ColorModulator wird als Einstellungs-Träger benutzt:
//   x = Wiederholungen rund um den Horizont
//   y = Helligkeit (0..1)
//   z = Drehung (0..1, eine volle Umdrehung)
void main() {
    vec3 dir = normalize(viewRay);
    float repeatCount = max(ColorModulator.x, 1.0);
    float brightness = ColorModulator.y;
    float spin = ColorModulator.z;

    // Waagerecht: Himmelsrichtung, senkrecht: Höhe über dem Horizont
    float u = (atan(dir.z, dir.x) / (2.0 * PI) + 0.5 + spin) * repeatCount;
    float elevation = asin(clamp(dir.y, -1.0, 1.0));      // -90°..+90° in Bogenmaß

    // Bild reicht von ~10° unter dem Horizont bis ~70° darüber
    const float bottom = -0.17;
    const float top = 1.22;
    float v = 1.0 - clamp((elevation - bottom) / (top - bottom), 0.0, 1.0);

    vec3 image = texture(Sampler0, vec2(u, v)).rgb;

    // weicher Übergang in tiefes Rot-Schwarz am Zenit und unter dem Horizont
    vec3 zenith = vec3(0.09, 0.02, 0.04);
    vec3 abyss = vec3(0.04, 0.0, 0.015);
    vec3 color = mix(image, zenith, smoothstep(top - 0.25, top + 0.1, elevation));
    color = mix(color, abyss, smoothstep(bottom + 0.1, bottom - 0.25, elevation));

    fragColor = vec4(color * brightness, 1.0);
}
