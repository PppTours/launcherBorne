#version 330 core

out vec4 color;

const vec3 colors[] = vec3[](
	vec3(0.224, 0.333, 0.678),
	vec3(0.259, 0.698, 0.565),
	vec3(0.259, 0.698, 0.565),
	vec3(0.333, 0.694, 0.522),
	vec3(0.333, 0.694, 0.522),
	vec3(0.353, 0.431, 0.714),
	vec3(0.369, 0.471, 0.706),
	vec3(0.416, 0.694, 0.663),
	vec3(0.416, 0.694, 0.663)
);

in vec3 v_normal;
in vec3 v_worldpos;
flat in uint v_objectid;

const vec3 sun = normalize(vec3(.3, .5, -1));

void main() {
	float light = 0;
	light = .2; // ambiant
	light += max(0, dot(v_normal, sun)); // diffuse
	light += .5*smoothstep(.5, 0, dot(v_worldpos, v_worldpos)); // """specular"""
	color = vec4(light*colors[v_objectid-1u], 1);
}