% primjer korištenog učenja neuronskih mreža
load test_categories_matlab.txt
load train_categories_matlab.txt
load features/profile_test.txt
load features/profile_train.txt
net = newff(profile_train', train_categories_matlab', 4, {'logsig', 'logsig'}, 'trainbr', 'learngdm', 'mse', {}, {}, 'dividerand');
[net, tr] = train(net, profile_train', train_categories_matlab');
simpleclassOutputs = sim(net, profile_test'); plotconfusion(test_categories_matlab', simpleclassOutputs);

w1 = net.IW{1};
b1 = net.b{1};
w2 = net.LW{2};
b2 = net.b{2};
save('w1.dat', 'w1', '-ascii');
save('b1.dat', 'b1', '-ascii');
save('w2.dat', 'w2', '-ascii');
save('b2.dat', 'b2', '-ascii');