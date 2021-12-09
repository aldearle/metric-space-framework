function d = csd(A,B)

m1 = sqrt(sum(A .* A));
m2 = sqrt(sum(B .* B));
product = sum(A .* B);

angle = acos(product / (m1 * m2));
adjAngle = angle  / (pi/2);
d = adjAngle;
% d=sin(angle);

end