#version 330 core

in vec2 i_position;
in vec2 i_uv;

out vec2 v_uv;

uniform mat4 u_camera;

void main() {
	gl_Position = u_camera*vec4(i_position, 0, 1);
	v_uv = i_uv;
}
