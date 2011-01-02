%%
train_set = histogram_train;
test_set = histogram_test;
net=newrb(train_set', train_categories_matlab', 0.02, 0.5, 300, 20);
%%
y=sim(net, train_set');
m=max(y);
y2=y;
for i = 1:size(y,2)
    y2(:,i)=(y2(:,i)==m(i));
end
length(find(y2~=train_categories_matlab'))
%%
y=sim(net, test_set');
m=max(y);
y2=y;
for i = 1:size(y,2)
    y2(:,i)=(y2(:,i)==m(i));
end
length(find(y2~=test_categories_matlab'))
%%
centri = [net.IW{1} net.b{1}]';
dlmwrite('learning/centri.txt', centri, ' ')
dlmwrite('learning/centri_dim.txt', size(centri), ' ')
tezine = [net.b{2} net.LW{2,1}];
dlmwrite('learning/tezine.txt', tezine, ' ')
dlmwrite('learning/tezine_dim.txt', size(tezine), ' ')
